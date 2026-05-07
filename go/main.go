package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/redis/go-redis/v9"
)

var (
	ctx = context.Background()
	rdb *redis.Client
)

const (
	alphabet    = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	base        = uint64(len(alphabet))
	poolKey     = "url_pool"
	counterKey  = "url_counter"
	poolSize    = 10000
	fillBatch   = 5000 
)

func encode(number uint64) string {
	if number == 0 {
		return string(alphabet[0])
	}
	var builder strings.Builder
	for number > 0 {
		builder.WriteByte(alphabet[number%base])
		number /= base
	}
	return builder.String()
}

func fillPoolWorker() {
	for {
		length, err := rdb.LLen(ctx, poolKey).Result()
		if err != nil {
			log.Printf("Error checking pool length: %v", err)
			time.Sleep(2 * time.Second)
			continue
		}

		if length < fillBatch {
			log.Printf("Pool is low (%d). Generating new batch...", length)
			
			lastID, err := rdb.IncrBy(ctx, counterKey, int64(poolSize)).Result()
			if err != nil {
				log.Printf("IncrBy error: %v", err)
				continue
			}

			codes := make([]interface{}, poolSize)
			for i := 0; i < poolSize; i++ {
				currentID := uint64(lastID) - uint64(poolSize) + uint64(i) + 1
				codes[i] = encode(currentID)
			}

			err = rdb.RPush(ctx, poolKey, codes...).Err()
			if err != nil {
				log.Printf("RPush error: %v", err)
			}
		}
		time.Sleep(5 * time.Second)
	}
}

type ShortenRequest struct {
	URL string `json:"url"`
}

func shortenHandler(w http.ResponseWriter, r *http.Request) {
	var req ShortenRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request", http.StatusBadRequest)
		return
	}

	shortCode, err := rdb.LPop(ctx, poolKey).Result()
	if err == redis.Nil {
		http.Error(w, "Pool is empty, try again later", http.StatusServiceUnavailable)
		return
	} else if err != nil {
		http.Error(w, "Redis error", http.StatusInternalServerError)
		return
	}

	err = rdb.Set(ctx, "short:"+shortCode, req.URL, 0).Err()
	if err != nil {
		http.Error(w, "Failed to save URL", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"short_url":  shortCode,
		"original":   req.URL,
	})
}

func main() {
	rdb = redis.NewClient(&redis.Options{
		Addr: "localhost:6379",
	})

	go fillPoolWorker()

	http.HandleFunc("/shorten", shortenHandler)

	fmt.Printf("Service started. Pre-generating up to %d codes...\n", poolSize)
	log.Fatal(http.ListenAndServe(":8080", nil))
}
#!/bin/bash

URL="http://localhost:8080/api/v1/admin/cards/1"
HEADERS="Accept: application/json"

for i in {1..100}
do
  echo "Sending request $i..."
  curl -s -o /dev/null -w "Request $i → HTTP %{http_code}\n" -X POST "$URL" -H "$HEADERS"
done

echo "✅ Done! Sent 100 POST requests."
curl --location --request POST 'http://localhost:9192/api/v1/data/assets' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "asset": {
    "properties": {
      "asset:prop:id": "194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae",
      "asset:prop:name": "Asset to push pcf data",
      "asset:prop:contenttype": "application/json",
      "asset:prop:policy-id": "use-eu",
      "asset:prop:type": "pcf-push"
    }
  },
  "dataAddress": {
    "properties": {
      "endpoint": "https://anything.lambda-url.eu-central-1.on.aws",
      "proxyMethod": true,
      "proxyBody": true,
      "proxyPath": true,
      "proxyQueryParams": true,
      "type": "HttpData"
    }
  }
}'

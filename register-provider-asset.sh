curl --location --request POST 'http://localhost:7172/api/v1/data/assets' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "asset": {
    "properties": {
      "asset:prop:id": "a4b0fa29-f344-4ad4-819a-4ffb3271709f-e5a77b76-d9eb-442b-b200-40756e85e332",
      "asset:prop:name": "Asset to request pcf data",
      "asset:prop:contenttype": "application/json",
      "asset:prop:policy-id": "use-eu",
      "asset:prop:type": "pcf-request"
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

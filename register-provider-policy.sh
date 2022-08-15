curl --location --request POST 'http://localhost:7172/api/v1/data/policies' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
    "uid": "14b5b0a0-ed26-4638-bef2-ad26e73a5b54",
    "policy": {
        "permissions": [
            {
                "edctype": "dataspaceconnector:permission",
                "target": "a4b0fa29-f344-4ad4-819a-4ffb3271709f-e5a77b76-d9eb-442b-b200-40756e85e332",
                "action": {
                    "type": "USE"
                }
            }
        ],
        "target": "a4b0fa29-f344-4ad4-819a-4ffb3271709f-e5a77b76-d9eb-442b-b200-40756e85e332"
    }
  }'

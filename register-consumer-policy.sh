curl --location --request POST 'http://localhost:9192/api/v1/data/policies' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
    "uid": "261acdc1-84db-4ad6-8156-af8bdcdace58",
    "policy": {
        "permissions": [
            {
                "edctype": "dataspaceconnector:permission",
                "target": "194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae",
                "action": {
                    "type": "USE"
                }
            }
        ],
        "target": "194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae"
    }
  }'

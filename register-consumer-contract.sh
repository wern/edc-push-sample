curl --location --request POST 'localhost:9192/api/v1/data/contractdefinitions/' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "1",
    "accessPolicyId": "261acdc1-84db-4ad6-8156-af8bdcdace58",
    "contractPolicyId": "261acdc1-84db-4ad6-8156-af8bdcdace58",
    "criteria": []
}
'

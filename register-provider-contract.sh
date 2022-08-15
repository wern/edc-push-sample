curl --location --request POST 'localhost:7172/api/v1/data/contractdefinitions/' \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "1",
    "accessPolicyId": "14b5b0a0-ed26-4638-bef2-ad26e73a5b54",
    "contractPolicyId": "14b5b0a0-ed26-4638-bef2-ad26e73a5b54",
    "criteria": []
}
'

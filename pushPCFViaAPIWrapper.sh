curl --location --request POST 'http://localhost:3131/api/service/194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae/productId/some?BPN=BPNL6352416&requestId=24356C6&provider-connector-url=http://consumer-controlplane:9193' \
--header 'Authorization: Basic c29tZXVzZXI6cGFzc3dvcmQ=' \
--header 'Content-Type: application/json' \
--data-raw '{
    "data": {
        "pcf": {
            "carbonEmissions": "0.47",
            "biogenicEmissions": "0.01",
            "landUseEmissions": "0.01",
            "reportingPeriodStart": "2020-07-01T00:00:00.000Z",
            "reportingPeriodEnd": "2021-07-01T00:00:00.000Z",
            "geographyCountry": "EU",
            "geographySubregion": null,
            "primaryDataShare": 100,
            "emissionFactorSources": [],
            "boundaryProcesses": "End-of-life included",
            "boundaryGate": "Cradle-to-gate",
            "crossSectoralStandardsUsed": [
                "ISO Standard 14044"
            ],
            "productSpecificRules": [],
            "allocationRules": null
        },
        "id": "639HR-F",
        "version": "9.6.3",
        "companyName": "Solvay",
        "companyId": [
            "urn:epc:id:fbrx:4346073.00000.4"
        ],
        "productDescription": "production",
        "productId": [
            "urn:epc:id:vasy:7494489.00000.9"
        ],
        "productCategoryCpc": "1620",
        "productNameCompany": "Chlorine",
        "declaredUnit": "kg",
        "declaredUnitAmount": "1",
        "waterContent": 1,
        "comment": ""
    }
}'

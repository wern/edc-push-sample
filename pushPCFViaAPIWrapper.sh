curl --location --request PUT 'http://localhost:3131/api/service/194f0160-cef1-4cb3-a6f1-1d917b4e39c0-fec89cf0-9382-4d68-8cea-dae797b7b8ae/productId/some?BPN=BPNL6352416&requestId=24356C6&provider-connector-url=http://consumer-controlplane:9193' \
--header 'X-ApiKey APIpassword' \
--header 'Content-Type: application/json' \
--data-raw '{
    "data": {
        "id": "3893bb5d-da16-4dc1-9185-11d97476c254",
        "specVersion": "1.0.0",
        "version": 42,
        "created": "2022-05-22T21:47:32Z",
        "updated": "2022-05-22T21:47:35Z",
        "companyName": "My Corp",
        "companyIds": [
            "urn:uuid:51131FB5-42A2-4267-A402-0ECFEFAD1619",
            "urn:epc:id:sgln:4063973.00000.8"
        ],
        "productDescription": "Cote’d Or Ethanol",
        "productIds": [
            "urn:gtin:4712345060507"
        ],
        "productCategoryCpc": "3342",
        "productNameCompany": "Green Ethanol Volnay",
        "comment": "",
        "pcf": {
            "declaredUnit": "liter",
            "unitaryProductAmount": "12.0",
            "fossilGhgEmissions": "0.123",
            "biogenicEmissions": {
                "landUseEmissions": "0.001",
                "otherEmissions": "0"
            },
            "biogenicCarbonContent": "0.0",
            "reportingPeriodStart": "2021-01-01T00:00:00Z",
            "reportingPeriodEnd": "2022-01-01T00:00:00Z",
            "primaryDataShare": 56.12,
            "emissionFactorSources": [
                {
                    "name": "Ecoinvent",
                    "version": "1.2.3"
                }
            ],
            "boundaryProcessesDescription": "End-of-life included",
            "crossSectoralStandardsUsed": [
                "GHG Protocol Product standard"
            ],
            "productOrSectorSpecificRules": [
                {
                    "operator": "EPD International",
                    "ruleNames": [
                        "ABC 2021"
                    ]
                }
            ]
        }
    }
}'
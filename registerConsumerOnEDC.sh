echo 'Registering asset...'
./register-consumer-asset.sh
echo 'Done.'

echo 'Registering policy...'
./register-consumer-policy.sh
echo 'Done.'

echo 'Registering contract...'
./register-consumer-contract.sh
echo 'Done.'

echo 'You can now send PCF via ./pushPCFviaAPIWrapper.sh'

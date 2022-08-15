echo 'Registering asset...'
./register-provider-asset.sh
echo 'Done.'

echo 'Registering policy...'
./register-provider-policy.sh
echo 'Done.'

echo 'Registering contract...'
./register-provider-contract.sh
echo 'Done.'

echo 'You can now request PCF via ./requestPCFviaAPIWrapper.sh'

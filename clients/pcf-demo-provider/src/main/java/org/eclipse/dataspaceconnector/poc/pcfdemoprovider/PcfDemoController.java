package org.eclipse.dataspaceconnector.poc.pcfdemoprovider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class PcfDemoController {

    @GetMapping("/productIds/{productId}")
    public ResponseEntity<String> requestPCF(@RequestHeader (name="Authorization") Optional<String> token, 
                                                                @PathVariable("productId") Optional<String> materialId,
                                                                @RequestParam("BPN") Optional<String> bpn,
                                                                @RequestParam("requestId") Optional<String> requestId,
                                                                @RequestParam("message") Optional<String> message){
        System.out.println("Received token " + token);
        if(token.isPresent()){
            if (materialId.isPresent()) {
                System.out.println("Received request for materialId " + materialId.get() + "from BPN '" + bpn.orElse("unknown") + "' with requestId '" + requestId.orElse("none") + "'");
                System.out.println("Additional message '" + message.orElse("No message provided") + "'");
                return ResponseEntity.accepted().build();
            } else {
                System.out.println("BAD_REQUEST!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            System.out.println("UNAUTHORIZED!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }  
    }

    @PutMapping("/productIds/{productId}")
    public ResponseEntity<String> receivePCF(@RequestHeader (name="Authorization") Optional<String> token, 
                                                                @PathVariable("productId") Optional<String> materialId,
                                                                @RequestParam("BPN") Optional<String> bpn,
                                                                @RequestParam("requestId") Optional<String> requestId,
                                                                @RequestBody Optional<Map<String,Object>> pcfValue){
        System.out.println("Received token " + token);
        if(token.isPresent()){
            if (materialId.isPresent()) {
                System.out.println("Received PCF value for materialId " + materialId.get() + "from BPN '" + bpn.orElse("unknown") + "' with requestId '" + requestId.orElse("none") + "'");
                System.out.println("PCF value: " + pcfValue.orElse(Collections.emptyMap()));
                return ResponseEntity.ok().build();
            } else {
                System.out.println("BAD_REQUEST!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            System.out.println("UNAUTHORIZED!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }  
    }

    @PatchMapping("/productIds/{productId}")
    public ResponseEntity<String> sendPCF(@RequestHeader (name="Authorization") Optional<String> token, 
                                                                @PathVariable("productId") Optional<String> materialId,
                                                                @RequestParam("BPN") Optional<String> bpn,
                                                                @RequestParam("requestId") Optional<String> requestId,
                                                                @RequestBody Optional<String> pcfValue){
        
        var url = System.getProperty("api.wrapper.url", "http://localhost:5050/productIds/$productId");
        System.out.println("Received token " + token);

        if(url == null){
            System.out.println("No URL configured!");
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }

        url = url.replace("$productId", materialId.orElse("xy000000Z"));
        url = url.replace("$BPN", bpn.orElse("BPN0000000L"));
        url = url.replace("$requestId", requestId.orElse("00000"));

        System.out.println("Using URL: " + url);

        if(token.isPresent()){
            if (materialId.isPresent()) {
                System.out.println("Sending PCF value for materialId " + materialId.get() + "from BPN '" + bpn.orElse("unknown") + "' with requestId '" + requestId.orElse("none") + "'");
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();    
                headers.set("X-Api-Key", token.get());
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(pcfValue.orElse("{}"), headers);
                restTemplate.put(url, entity);

                return ResponseEntity.ok().build();
            } else {
                System.out.println("BAD_REQUEST!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            System.out.println("UNAUTHORIZED!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }  
    }
}

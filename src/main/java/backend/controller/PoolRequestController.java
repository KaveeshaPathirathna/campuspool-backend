package backend.controller;

import backend.entity.PoolRequest;
import backend.service.PoolRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pool-requests")
@CrossOrigin(origins = "*")
public class PoolRequestController {

    @Autowired
    private PoolRequestService poolRequestService;

    @PostMapping
    public ResponseEntity<PoolRequest> createRequest(@RequestBody PoolRequest request) {
        PoolRequest savedRequest = poolRequestService.createRequest(request);
        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<PoolRequest>> getAllRequests() {
        return new ResponseEntity<>(poolRequestService.getAllRequests(), HttpStatus.OK);
    }
}
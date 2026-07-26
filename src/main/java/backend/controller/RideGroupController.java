package backend.controller;

import backend.entity.RideGroup;
import backend.repository.RideGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class RideGroupController {

    @Autowired
    private RideGroupRepository rideGroupRepository;

    @GetMapping("/active")
    public ResponseEntity<List<RideGroup>> getActiveGroups() {
        return ResponseEntity.ok(rideGroupRepository.findAll());
    }
}
package backend.service;

import backend.entity.PoolRequest;
import backend.repository.PoolRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PoolRequestService {

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    public PoolRequest createRequest(PoolRequest request) {
        request.setStatus("PENDING");
        return poolRequestRepository.save(request);
    }

    public List<PoolRequest> getAllRequests() {
        return poolRequestRepository.findAll();
    }
}
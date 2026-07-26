package backend.service;

import backend.entity.PoolRequest;
import backend.entity.Student;
import backend.repository.FriendRepository;
import backend.repository.PoolRequestRepository;
import backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalFriendConnections", friendRepository.count());
        stats.put("totalPoolRequests", poolRequestRepository.count());
        return stats;
    }

    @Override
    @Transactional
    public boolean deleteStudent(String studentId) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isPresent()) {
            studentRepository.delete(studentOpt.get());
            return true;
        }
        return false;
    }

    @Override
    public List<PoolRequest> getAllPoolRequests() {
        return poolRequestRepository.findAll();
    }

    // ✅ Implementation of deleting pool request by ID
    @Override
    @Transactional
    public boolean deletePoolRequest(Long requestId) {
        Optional<PoolRequest> poolOpt = poolRequestRepository.findById(requestId);
        if (poolOpt.isPresent()) {
            poolRequestRepository.delete(poolOpt.get());
            return true;
        }
        return false;
    }
}
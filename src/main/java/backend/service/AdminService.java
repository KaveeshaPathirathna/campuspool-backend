package backend.service;

import backend.entity.PoolRequest;
import backend.entity.Student;
import java.util.List;
import java.util.Map;

public interface AdminService {
    List<Student> getAllStudents();
    Map<String, Object> getDashboardStats();
    boolean deleteStudent(String studentId);
    List<PoolRequest> getAllPoolRequests();

    // ✅ Pool Request/Group delete feature
    boolean deletePoolRequest(Long requestId);
}
package il.co.topq.report.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStateRepository extends JpaRepository<ExecutionState, Integer>{
	
	List<ExecutionState> findByActive(boolean active);
	
}


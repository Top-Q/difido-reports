package il.co.topq.report.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface MetadataRepository extends JpaRepository<ExecutionMetadata, Integer> {
	
	List<ExecutionMetadata> findByShared(boolean shared);
	
	List<ExecutionMetadata> findByActive(boolean active);
	
	List<ExecutionMetadata> findBySharedIsTrueAndActiveIsTrue();
	
	ExecutionMetadata findById(int id);
	
	List<ExecutionMetadata> findByOrderByIdAsc();
	
	List<ExecutionMetadata> findByOrderByIdDesc();
	
}

package il.co.topq.report.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import il.co.topq.report.business.execution.ExecutionMetadata;

public interface MetadataRepository extends JpaRepository<ExecutionMetadata, Integer> {

	List<ExecutionMetadata> findByShared(boolean shared);

	ExecutionMetadata findById(int id);

	List<ExecutionMetadata> findByOrderByIdAsc();

	List<ExecutionMetadata> findByOrderByIdDesc();

	List<ExecutionMetadata> findAllByTimestampBetween(Date timestampStart, Date timestampEnd);

	@Query("select a from ExecutionMetadata a where a.timestamp <= :to")
	List<ExecutionMetadata> findAllWithTimestampBefore(@Param("to") Date timestampEnd);

	@Query("select a from ExecutionMetadata a where a.timestamp >= :from")
	List<ExecutionMetadata> findAllWithTimestampAfter(@Param("from") Date timestampStart);
	
}

package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Long> {
    List<Size> findBySizeGroupIdAndActiveTrueOrderBySortOrderAsc(Long sizeGroupId);
    List<Size> findByActiveTrueOrderBySizeGroupIdAscSortOrderAsc();
    boolean existsBySizeGroupIdAndSizeCode(Long sizeGroupId, String sizeCode);
    Optional<Size> findBySizeCode(String sizeCode);
}

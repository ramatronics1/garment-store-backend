package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.StoreMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreMediaRepository extends JpaRepository<StoreMedia, Long> {

    Optional<StoreMedia> findByPlacementKey(String placementKey);

    /** All active slots whose key starts with a given prefix, ordered by display_order. */
    List<StoreMedia> findByPlacementKeyStartingWithAndActiveTrueOrderByDisplayOrderAsc(String prefix);

    /** All slots (active + inactive) for the admin management view. */
    List<StoreMedia> findAllByOrderByDisplayOrderAscPlacementKeyAsc();
}

package com.warehouse.dao;

import com.warehouse.model.Good;
import java.util.List;

/** Data access contract for {@link Good} (nomenclature) entities. */
public interface GoodDAO extends BaseDAO<Good, Integer> {

    List<Good> findAllActive();

    /** Returns goods whose quantity is at or below their min_threshold. */
    List<Good> findBelowThreshold();

    /** Case-insensitive name / category search. */
    List<Good> searchByNameOrCategory(String keyword);

    /**
     * Atomically adjusts the quantity in the DB (positive = add, negative =
     * remove).
     */
    void adjustQuantity(int goodId, int delta);
}

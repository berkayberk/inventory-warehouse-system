package com.warehouse.dao;

import com.warehouse.model.Supplier;
import java.util.List;

/** Data access contract for {@link Supplier} entities. */
public interface SupplierDAO extends BaseDAO<Supplier, Integer> {

    /** Returns all active (non-deleted) suppliers. */
    List<Supplier> findAllActive();

    /** Case-insensitive name search. */
    List<Supplier> searchByName(String keyword);
}

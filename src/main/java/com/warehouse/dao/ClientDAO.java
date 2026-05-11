package com.warehouse.dao;

import com.warehouse.model.Client;
import java.util.List;

/** Data access contract for {@link Client} entities. */
public interface ClientDAO extends BaseDAO<Client, Integer> {

    List<Client> findAllActive();

    List<Client> searchByName(String keyword);
}

package com.warehouse.dao;

import com.warehouse.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for {@link User} entities.
 */
public interface UserDAO extends BaseDAO<User, Integer> {

    /** Looks up a user by their login username. */
    Optional<User> findByUsername(String username);

    /** Returns all active users. */
    List<User> findAllActive();
}

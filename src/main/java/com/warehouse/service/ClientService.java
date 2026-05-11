package com.warehouse.service;

import com.warehouse.dao.ClientDAO;
import com.warehouse.dao.impl.ClientDAOImpl;
import com.warehouse.model.Client;
import com.warehouse.util.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/** Business logic for client management. Only admins may create/edit/delete. */
public class ClientService {

    private static final Logger LOG = LogManager.getLogger(ClientService.class);

    private final ClientDAO clientDAO;
    private final ActivityLogService logService;

    public ClientService() {
        this.clientDAO = new ClientDAOImpl();
        this.logService = new ActivityLogService();
    }

    public ClientService(ClientDAO clientDAO, ActivityLogService logService) {
        this.clientDAO = clientDAO;
        this.logService = logService;
    }

    public Client create(String name, String contact, String address,
            String phone, String email) {
        requireAdmin();
        Client c = new Client(name, contact, address, phone, email);
        clientDAO.save(c);
        logActor("CREATE_CLIENT", "Created client: " + name);
        LOG.info("Client created: {}", name);
        return c;
    }

    public void update(Client client) {
        requireAdmin();
        clientDAO.update(client);
        logActor("UPDATE_CLIENT", "Updated client id=" + client.getId());
    }

    public void delete(int id) {
        requireAdmin();
        clientDAO.delete(id);
        logActor("DELETE_CLIENT", "Deleted client id=" + id);
    }

    public List<Client> findAll() {
        return clientDAO.findAll();
    }

    public List<Client> findAllActive() {
        return clientDAO.findAllActive();
    }

    public List<Client> search(String kw) {
        return clientDAO.searchByName(kw);
    }

    public Optional<Client> findById(int id) {
        return clientDAO.findById(id);
    }

    private void requireAdmin() {
        if (!SessionManager.isAdmin())
            throw new SecurityException("Only administrators may manage clients.");
    }

    private void logActor(String action, String details) {
        var u = SessionManager.getCurrentUser();
        if (u != null)
            logService.log(u.getId(), u.getUsername(), action, details);
    }
}

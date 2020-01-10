package ru.vegd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.vegd.dao.TagDAO;
import ru.vegd.entity.Tag;
import ru.vegd.service.TagService;

import java.sql.SQLException;
import java.util.List;

public class TagServiceImpl implements TagService {

    @Autowired
    TagDAO tagDAO;

    @Override
    public List getAll() throws SQLException {
        return tagDAO.getAll();
    }

    @Override
    public void add(Tag tag) throws SQLException {
        tagDAO.add(tag);
    }

    @Override
    public Tag read(Long ID) throws SQLException {
        return tagDAO.read(ID);
    }

    @Override
    public void delete(Long ID) throws SQLException {
        tagDAO.delete(ID);
    }

    @Override
    public void update(Tag tag) throws SQLException {
        tagDAO.update(tag);
    }

}

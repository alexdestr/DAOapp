package ru.vegd.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.vegd.dao.NewsDAO;
import ru.vegd.entity.News;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NewsDAOImpl implements NewsDAO {

    private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NewsDAOImpl.class.getName());

    @Autowired
    private DataSource dataSource;

    private static final String SQL_GETALL = "SELECT news_id, author_id, author_name, title, news_text, creation_date " +
            "FROM \"news\"";
    private static final String SQL_GET_PAGINATED_NEWS = "WITH news " +
            "AS (SELECT ROW_NUMBER() OVER (ORDER BY creation_date DESC) " +
            "AS row_id, news_id, author_id, author_name, title, news_text, creation_date FROM news) " +
            "SELECT news_id, author_id, author_name, title, news_text, creation_date " +
            "FROM news " +
            "WHERE row_id " +
            "BETWEEN ? AND ?";
    private static final String SQL_GET_PAGINATED_NEWS_BY_SEARCH = "WITH news " +
            "AS (SELECT ROW_NUMBER() OVER (ORDER BY creation_date DESC) " +
            "AS row_id, news_id, author_id, author_name, title, news_text, creation_date FROM news) " +
            "SELECT news_id, author_id, author_name, title, news_text, creation_date " +
            "FROM news " +
            "WHERE (row_id BETWEEN ? AND ?)" +
            "AND to_tsvector(title) @@ to_tsquery(?)";
    private static final String SQL_GET_NUMBER_NEWS = "SELECT COUNT(*)" +
            "FROM news " +
            "WHERE news_id > 0";
    private static final String SQL_ADD = "INSERT " +
            "INTO \"news\" (author_id, author_name, title, news_text, creation_date) " +
            "VALUES ( ?, ?, ?, ?, ?)";
    private static final String SQL_READ = "SELECT news_id, author_id, author_name, title, news_text, creation_date " +
            "FROM \"news\" " +
            "WHERE news_id = ?";
    private static final String SQL_DELETE = "DELETE " +
            "FROM \"news\" " +
            "WHERE news.\"news_id\" = ?";
    private static final String SQL_UPDATE = "UPDATE \"news\" " +
            "SET author_id = ?, title = ?, news_text = ? " +
            "WHERE news_id = ?";

    @Override
    public List getAll() throws SQLException {
        List<News> newsList = new ArrayList<>();
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_GETALL);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                News aNews = new News();

                aNews.setNewsId(resultSet.getLong("news_id"));
                aNews.setAuthorId(resultSet.getLong("author_id"));
                aNews.setAuthorName(resultSet.getString("author_name"));
                aNews.setTitle(resultSet.getString("title"));
                aNews.setNewsText(resultSet.getString("news_text"));
                aNews.setPublicDate(resultSet.getTimestamp("creation_date"));

                newsList.add(aNews);
            }

        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }

        return newsList;
    }

    @Override
    public List getPaginatedNews(Long beginIndex, Long endIndex) throws SQLException {
        List<News> newsList = new ArrayList<>();
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_GET_PAGINATED_NEWS);

            preparedStatement.setLong(1, beginIndex);
            preparedStatement.setLong(2, endIndex);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                News aNews = new News();

                aNews.setNewsId(resultSet.getLong("news_id"));
                aNews.setAuthorId(resultSet.getLong("author_id"));
                aNews.setAuthorName(resultSet.getString("author_name"));
                aNews.setTitle(resultSet.getString("title"));
                aNews.setNewsText(resultSet.getString("news_text"));
                aNews.setPublicDate(resultSet.getTimestamp("creation_date"));

                newsList.add(aNews);
            }
        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return newsList;
    }

    @Override
    public List getPaginatedNewsBySearch(Long beginIndex, Long endIndex, String searchText) throws SQLException {
        List<News> newsList = new ArrayList<>();
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_GET_PAGINATED_NEWS_BY_SEARCH);

            preparedStatement.setLong(1, beginIndex);
            preparedStatement.setLong(2, endIndex);
            preparedStatement.setString(3, "'" + searchText + "'");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                News aNews = new News();

                aNews.setNewsId(resultSet.getLong("news_id"));
                aNews.setAuthorId(resultSet.getLong("author_id"));
                aNews.setAuthorName(resultSet.getString("author_name"));
                aNews.setTitle(resultSet.getString("title"));
                aNews.setNewsText(resultSet.getString("news_text"));
                aNews.setPublicDate(resultSet.getTimestamp("creation_date"));

                newsList.add(aNews);
            }
        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return newsList;
    }

    @Override
    public Long getNumberNews() throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_GET_NUMBER_NEWS);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return 0L;
    }

    @Override
    public Long add(News aNews) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;
        Long lastInsertedNewsID = -1L;

        try {
            preparedStatement = connection.prepareStatement(SQL_ADD, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setLong(1, aNews.getAuthorId());
            preparedStatement.setString(2, aNews.getAuthorName());
            preparedStatement.setString(3, aNews.getTitle());
            preparedStatement.setString(4, aNews.getNewsText());
            preparedStatement.setTimestamp(5, aNews.getPublicDate());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                lastInsertedNewsID = resultSet.getLong(1);
            }
            logger.info("Success adding");

        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return lastInsertedNewsID;
    }

    @Override
    public News read(Long ID) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;
        News news = new News();

        try {
            preparedStatement = connection.prepareStatement(SQL_READ);
            preparedStatement.setLong(1, ID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                news.setNewsId(resultSet.getLong("news_id"));
                news.setAuthorId(resultSet.getLong("author_id"));
                news.setAuthorName(resultSet.getString("author_name"));
                news.setTitle(resultSet.getString("title"));
                news.setNewsText(resultSet.getString("news_text"));
                news.setPublicDate(resultSet.getTimestamp("creation_date"));
            }

        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return news;
    }

    @Override
    public void delete(Long ID) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_DELETE);
            preparedStatement.setLong(1, ID);
            preparedStatement.executeUpdate();
            logger.info("Success delete");

        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public void update(News aNews) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(SQL_UPDATE);

            preparedStatement.setLong(1, aNews.getAuthorId());
            preparedStatement.setString(2, aNews.getTitle());
            preparedStatement.setString(3, aNews.getNewsText());
            preparedStatement.setLong(4, aNews.getNewsId());

            preparedStatement.executeUpdate();
            logger.info("Success update");

        } catch (SQLException e) {
            logger.warn("Request eror");
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

}

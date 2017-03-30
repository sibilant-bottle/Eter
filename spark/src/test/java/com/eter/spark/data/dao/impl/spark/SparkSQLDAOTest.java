package com.eter.spark.data.dao.impl.spark;

import com.eter.spark.data.database.DatabaseProperties;
import com.eter.spark.data.database.impl.spark.SparkSQLConnection;
import com.eter.spark.data.database.impl.spark.SparkSQLProperties;
import com.eter.spark.data.entity.Category;
import com.eter.spark.data.entity.Customer;
import com.eter.spark.data.entity.Product;
import com.eter.spark.data.util.transform.reflect.EntityReflection;
import com.eter.spark.data.util.transform.reflect.MethodSolver;
import com.eter.spark.data.util.dao.SparkSQLRelationResolver;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rusifer on 3/26/17.
 */
public class SparkSQLDAOTest {
    SparkSQLConnection connection;
    SparkSQLDAO dao;

    @Before
    public void setUp() throws Exception {
//        connection = new SparkSQLConnection();
//        DatabaseProperties properties = new SparkSQLProperties();
//        properties.put("url", "jdbc:postgresql://localhost:5432/testdata");
//        properties.put("user", "rusifer");
//        properties.put("password", "");
//        properties.put("warehouse-dir", "spark-warehouse");
//        properties.put("appName", "TEST");
//        properties.put("master", "local");
//        connection.applyProperties(properties);
//        connection.connect();
//        dao = new SparkSQLDAO();
//        dao.setDatabaseConnection(connection);
    }

    @Before
    public void setupSQLServer() throws Exception {

        connection = new SparkSQLConnection();
        DatabaseProperties properties = new SparkSQLProperties();
        properties.put("url", "jdbc:sqlserver://localhost:1433;databaseName=NC_testdata");
        properties.put("user", "onlyforreading");
        properties.put("password", "");
        properties.put("warehouse-dir", "spark-warehouse");
        properties.put("appName", "TEST");
        properties.put("master", "local");
        connection.applyProperties(properties);
        connection.connect();
        dao = new SparkSQLDAO();
        dao.setDatabaseConnection(connection);

    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void getAll() throws Exception {
        Dataset<Row> products = dao.getAllAsDataset(Product.class);

        SparkSQLRelationResolver.resolveOneToOne(dao, products, Product.class)
                .foreach((objectProduct) -> {
                    assert(objectProduct.getCategory() != null);
                });


        StructType productsSchema = EntityReflection.reflectEntityToSparkSchema(Product.class);
        StructType categorySchema = EntityReflection.reflectEntityToSparkSchema(Category.class);
        Dataset<Row> productsDataset = dao.getAndJoinAsDataset(Product.class);

        for(StructField field : productsSchema.fields()) {
            try {
                productsDataset.schema().fieldIndex(field.name());
            } catch (IllegalArgumentException e) {
                assert (false) : "Field '" + field.name() + "' doesn't exist";
            }
        }

        for(StructField field : categorySchema.fields()) {
            try {
                if (!field.name().equals("id"))
                    assert (productsDataset.schema().fieldIndex(field.name()) >= 0);
            } catch (IllegalArgumentException e) {
                assert (false) : "Field '" + field.name() + "' doesn't exist";
            }
        }
    }

}
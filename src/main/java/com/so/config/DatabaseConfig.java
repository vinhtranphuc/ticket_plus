package com.so.config;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.so.common.FieldMap;
import com.so.interceptor.MyBatisQueryIntercept;
import com.so.interceptor.MyBatisUpdateIntercept;


/**
 * Created by vinhtp on 2019-4-4.
 */
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties(DatabaseConfig.class)
@EnableTransactionManagement
@MapperScan(basePackages = "com.so.mybatis.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class DatabaseConfig {

	private org.apache.ibatis.session.Configuration configuration;

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
	    return new PersistenceExceptionTranslationPostProcessor();
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan(new String[] { "com.so.jpa.entity" });
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());
		return em;
	}
	
	@Bean
    public DataSource dataSource() {
    	DriverManagerDataSource dataSource = new DriverManagerDataSource();
    	dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    	dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/ticket_plus?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }
	
	private Properties additionalProperties() {
	    Properties properties = new Properties();
	    properties.setProperty("hibernate.hbm2ddl.auto", "update");
	    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
	    return properties;
	}
	
    @Bean
    public SqlSessionFactory sqlSessionFactory(ApplicationContext applicationContext) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        
        // mybatis config
        configuration = new org.apache.ibatis.session.Configuration();
        configuration.setCacheEnabled(false);
        configuration.setCallSettersOnNulls(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setDefaultExecutorType(ExecutorType.REUSE);
        //configuration.setMapUnderscoreToCamelCase(true);
        registTypeAlias("com.so.mybatis.model", FieldMap.class);
        
        sessionFactory.setConfiguration(configuration);
        
        //mybatis mapper config
        sessionFactory.setMapperLocations(applicationContext.getResources("classpath*:/mybatis/*Mapper.xml"));
        
        //data source
        sessionFactory.setDataSource(dataSource());
        
        MyBatisUpdateIntercept myBatisUpdateIntercept = new MyBatisUpdateIntercept();
        MyBatisQueryIntercept myBatisQueryIntercept = new MyBatisQueryIntercept();  
        sessionFactory.setPlugins(new Interceptor[] {myBatisUpdateIntercept});
        sessionFactory.setPlugins(new Interceptor[] {myBatisQueryIntercept});
        return sessionFactory.getObject();
    }

    private void registTypeAlias(final String packageScan,Class<?> ...classes) throws ClassNotFoundException {
    	final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    	provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
    	final Set<BeanDefinition> packageScanClasses = provider.findCandidateComponents(packageScan);
    	
    	// model bean
    	for (BeanDefinition bean: packageScanClasses) {
    	    Class<?> clazz = Class.forName(bean.getBeanClassName());
    	    configuration.getTypeAliasRegistry().registerAlias(clazz);
    	}
    	for(Class<?> clazz : classes){
		// register with lowerCase field
		 configuration.getTypeAliasRegistry().registerAlias(clazz);
	   }
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
	    JpaTransactionManager transactionManager = new JpaTransactionManager();
	    transactionManager.setEntityManagerFactory(emf);
	    return transactionManager;
	}

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}

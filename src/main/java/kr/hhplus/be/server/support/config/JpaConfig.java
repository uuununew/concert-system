package kr.hhplus.be.server.support.config;

/*@Configuration
@EnableJpaRepositories(basePackages = "kr.hhplus.be.server") // JPA 리포지토리 활성화
@EnableTransactionManagement // 트랜잭션 관리 활성화*/
public class JpaConfig {
/*    *//**
     * EntityManagerFactory 설정
     * - JPA에서 EntityManager를 관리
     *//*
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName("default");
        factoryBean.setPackagesToScan("kr.hhplus.be.server.domain");  // JPA 엔티티 클래스 패키지 설정
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return factoryBean;
    }

    *//**
     * 트랜잭션 관리 설정
     * - JPA 트랜잭션 관리
     *//*
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }*/
}

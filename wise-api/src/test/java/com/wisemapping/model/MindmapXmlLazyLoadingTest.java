package com.wisemapping.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wisemapping.config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.util.Calendar;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {AppConfig.class})
@Transactional
class MindmapXmlLazyLoadingTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void xmlPayloadIsLoadedLazily() throws Exception {
        final Account creator = entityManager.find(Account.class, 1);
        assertNotNull(creator, "Seed user must exist for the test");

        Mindmap mindmap = new Mindmap();
        mindmap.setCreator(creator);
        mindmap.setLastEditor(creator);
        mindmap.setTitle("Lazy Loading Map");
        mindmap.setDescription("Ensures XML table is lazy");
        mindmap.setCreationTime(Calendar.getInstance());
        mindmap.setLastModificationTime(Calendar.getInstance());
        mindmap.setPublic(false);
        mindmap.setXmlStr("<map version=\"tango\"><topic central=\"true\" text=\"Root\"/></map>");

        entityManager.persist(mindmap);
        entityManager.flush();
        final int mindmapId = mindmap.getId();
        entityManager.clear();

        final SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        final Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        statistics.setStatisticsEnabled(true);

        Mindmap reloaded = entityManager.find(Mindmap.class, mindmapId);
        EntityStatistics xmlStats = statistics.getEntityStatistics(MindmapXml.class.getName());
        assertEquals(0, xmlStats.getLoadCount(), "XML row must not load during Mindmap fetch");

        String xml = reloaded.getXmlStr();
        xmlStats = statistics.getEntityStatistics(MindmapXml.class.getName());
        assertTrue(xmlStats.getLoadCount() > 0, "Reading XML must trigger a MindmapXml load");
        assertTrue(xml.contains("Root"));
    }
}

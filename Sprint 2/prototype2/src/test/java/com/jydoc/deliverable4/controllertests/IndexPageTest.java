package com.jydoc.deliverable4.controllertests;

import com.jydoc.deliverable4.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
        }
)


@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class IndexPageTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    public void testIndexPageLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Welcome to Our Platform")));
    }


    // ============ CORE PAGE STRUCTURE TESTS ============
    @Test
    public void testPageMetadata() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(xpath("//meta[@charset='UTF-8']").exists())
                .andExpect(xpath("//meta[@name='viewport']").exists())
                .andExpect(xpath("//meta[@name='viewport'][@content='width=device-width, initial-scale=1.0']").exists())
                .andExpect(xpath("//title").string("Welcome to Our App"));
    }

    @Test
    public void testCSSResources() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Test for regular href attributes instead of th:href
                .andExpect(xpath("//link[@rel='stylesheet' and contains(@href,'styles.css')]").exists())
                .andExpect(xpath("//link[@rel='stylesheet' and contains(@href,'bootstrap.min.css')]").exists());
    }

    @Test
    public void testJSResources() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//script[contains(@src,'bootstrap.bundle.min.js')]").exists());
    }

    @Test
    public void testIconResources() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//link[@rel='stylesheet'][contains(@href,'bootstrap-icons.css')]").exists());
    }

    // ============ HEADER/FOOTER TESTS ============
    @Test
    public void testHeaderFragmentInclusion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Check for any recognizable header element
                .andExpect(xpath("//header | //*[contains(@class,'header') or contains(@class,'navbar') or contains(@class,'navigation')]").exists())

                // Check for any interactive element
                .andExpect(xpath("//header//*[@href or @onclick or @role='button'] | " +
                        "//*[contains(@class,'header')]//*[@href or @onclick or @role='button'] | " +
                        "//*[contains(@class,'navbar')]//*[@href or @onclick or @role='button']").exists());
    }

    @Test
    public void testFooterFragmentInclusion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Check for any footer container
                .andExpect(xpath("//*[contains(@class,'footer') or local-name()='footer']").exists())
                // Check for any copyright text (more flexible matching)
                .andExpect(xpath("//*[contains(@class,'footer') or local-name()='footer']" +
                        "//*[contains(translate(., '©', ''), 'Copyright') or " +
                        "contains(., 'All rights reserved')]").exists());
    }


    // ============ HERO SECTION TESTS ============
    @Test
    public void testHeroSectionStructure() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//div[contains(@class,'bg-white') and contains(@class,'rounded-3')]").exists())
                .andExpect(xpath("//h1[contains(@class,'display-5') and contains(text(),'Welcome to Our Platform')]").exists())
                .andExpect(xpath("//p[contains(@class,'fs-4') and contains(text(),'Manage your account')]").exists());
    }

    // ============ AUTHENTICATION-AWARE ELEMENT TESTS ============
    @Test
    @WithAnonymousUser
    public void testAnonymousUserElements() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Should be visible
                .andExpect(xpath("//a[contains(@href,'/register') and contains(@class,'btn-success')]").exists())
                .andExpect(xpath("//a[contains(@href,'/login') and contains(@class,'btn-outline-secondary')]").exists())
                .andExpect(xpath("//a[contains(@href,'/register') and contains(text(),'Create Account')]").exists())
                // Should be hidden
                .andExpect(xpath("//a[contains(@href,'/dashboard')]").doesNotExist())
                .andExpect(xpath("//form[contains(@action,'/logout')]").doesNotExist())
                .andExpect(xpath("//a[contains(text(),'Go to Dashboard')]").doesNotExist());
    }

    @Test
    @WithMockUser
    public void testAuthenticatedUserElements() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                // Should be visible for authenticated users
                .andExpect(xpath("//a[contains(@href,'/dashboard') and contains(@class,'btn-primary')]").exists())
                .andExpect(xpath("//form[contains(@action,'/logout')]").exists())
                .andExpect(xpath("//button[contains(@class,'btn-danger') and contains(.,'Logout')]").exists()) // Changed text() to .
                .andExpect(xpath("//a[contains(.,'Go to Dashboard')]").exists()) // Changed text() to .
                // Should be hidden for authenticated users
                .andExpect(xpath("//a[contains(@href,'/register') and contains(@class,'btn-success')]").doesNotExist())
                .andExpect(xpath("//a[contains(@href,'/login')]").doesNotExist())
                .andExpect(xpath("//a[contains(.,'Create Account')]").doesNotExist());
    }

    @Test
    @WithMockUser
    public void testLogoutFormCSRF() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//form[@action='/logout']/input[@type='hidden'][@name='_csrf']").exists());
    }

    // ============ FEATURE CARDS TESTS ============
    @Test
    public void testFeatureCardsStructure() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//div[contains(@class,'row') and contains(@class,'g-4')]").exists())
                // Only count col-md-6 divs within the features row
                .andExpect(xpath("//div[contains(@class,'row') and contains(@class,'g-4')]/div[contains(@class,'col-md-6')]").nodeCount(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Secure authentication",
            "User dashboard",
            "Responsive design"
    })
    public void testFeatureListItems(String feature) throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//li[contains(., '" + feature + "')]").exists());
    }

    @Test
    public void testFeatureIcons() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//i[contains(@class,'bi-check-circle-fill')]").nodeCount(3));
    }

    // ============ RESPONSIVE DESIGN TESTS ============
    @Test
    public void testResponsiveClasses() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//main[contains(@class,'container')]").exists())
                .andExpect(xpath("//div[contains(@class,'col-md-6')]").exists())
                .andExpect(xpath("//div[contains(@class,'d-sm-flex')]").exists());
    }

    // ============ ACCESSIBILITY TESTS ============
    @Test
    public void testButtonAriaAttributes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//a[contains(@class,'btn') and not(@aria-label)]").doesNotExist())
                .andExpect(xpath("//button[not(@aria-label)]").doesNotExist());
    }

    @Test
    public void testImagesHaveAltText() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(xpath("//img[not(@alt)]").doesNotExist());
    }

    // ============ SECURITY TESTS ============
    @Test
    public void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }

    // ============ PERFORMANCE TESTS ============
    @Test
    public void testPageLoadTime() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/")).andExpect(status().isOk());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Fail if page takes more than 2 seconds to load
        assert (duration < 2000) : "Page load time too slow: " + duration + "ms";
    }
}
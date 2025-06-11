package eu.efti.eftigate.controller;

import eu.efti.eftigate.service.RabbitSenderService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GateAdministrationController.class)
@ContextConfiguration(classes= {GateAdministrationController.class})
@ExtendWith(SpringExtension.class)
public class GateAdministrationControllerTest {

    @MockBean
    private GateAdministrationController gateAdministrationController;

    @Autowired
    protected MockMvc mockMvc;

    @Mock
    private RabbitSenderService rabbitSenderService;

    public GateAdministrationControllerTest() {
        super();
    }
}

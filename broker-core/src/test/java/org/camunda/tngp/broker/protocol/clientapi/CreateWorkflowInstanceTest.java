package org.camunda.tngp.broker.protocol.clientapi;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.tngp.test.broker.protocol.clientapi.ClientApiRule;
import org.camunda.tngp.test.broker.protocol.clientapi.ExecuteCommandResponse;
import org.camunda.tngp.test.broker.protocol.clientapi.SubscribedEvent;
import org.camunda.tngp.test.broker.protocol.clientapi.TestTopicClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.tngp.broker.workflow.data.WorkflowInstanceEvent.*;
import static org.camunda.tngp.broker.workflow.data.WorkflowInstanceEventType.START_EVENT_OCCURRED;
import static org.camunda.tngp.broker.workflow.data.WorkflowInstanceEventType.WORKFLOW_INSTANCE_CREATED;
import static org.camunda.tngp.test.broker.protocol.clientapi.TestTopicClient.workflowInstanceEvents;

public class CreateWorkflowInstanceTest
{
    private static final byte[] PAYLOAD = "payload".getBytes();

    public EmbeddedBrokerRule brokerRule = new EmbeddedBrokerRule();

    public ClientApiRule apiRule = new ClientApiRule();
    private TestTopicClient testClient;

    @Before
    public void init()
    {
        testClient = apiRule.topic(0);
    }

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(brokerRule).around(apiRule);

    @Test
    public void shouldRejectWorkflowInstanceCreation()
    {
        // when
        final ExecuteCommandResponse resp = testClient.sendCreateWorkflowInstanceRequest("process");

        // then
        assertThat(resp.key()).isGreaterThanOrEqualTo(0L);
        assertThat(resp.topicId()).isEqualTo(0L);
        assertThat(resp.getEvent())
            .containsEntry(PROP_EVENT_TYPE, "WORKFLOW_INSTANCE_REJECTED")
            .containsEntry(PROP_WORKFLOW_BPMN_PROCESS_ID, "process");

    }

    @Test
    public void shouldCreateWorkflowInstance()
    {
        // given
        testClient.deploy(Bpmn.createExecutableProcess("process")
                .startEvent()
                .endEvent()
                .done());

        // when
        final ExecuteCommandResponse resp = testClient.sendCreateWorkflowInstanceRequest("process");

        // then
        assertThat(resp.key()).isGreaterThanOrEqualTo(0L);
        assertThat(resp.topicId()).isEqualTo(0L);
        assertThat(resp.getEvent())
            .containsEntry(PROP_EVENT_TYPE, WORKFLOW_INSTANCE_CREATED.name())
            .containsEntry(PROP_WORKFLOW_BPMN_PROCESS_ID, "process")
            .containsEntry(PROP_WORKFLOW_INSTANCE_KEY, resp.key())
            .containsEntry(PROP_WORKFLOW_VERSION, 1);
    }

    @Test
    public void shouldCreateLatestVersionOfWorkflowInstance()
    {
        // given
        testClient.deploy(Bpmn.createExecutableProcess("process")
                .startEvent("foo")
                .endEvent()
                .done());

        testClient.deploy(Bpmn.createExecutableProcess("process")
                .startEvent("bar")
                .endEvent()
                .done());

        // when
        final ExecuteCommandResponse resp = testClient.sendCreateWorkflowInstanceRequest("process");

        // then
        final SubscribedEvent event = testClient.receiveSingleEvent(workflowInstanceEvents(START_EVENT_OCCURRED.name()));

        assertThat(event.event())
            .containsEntry(PROP_WORKFLOW_BPMN_PROCESS_ID, "process")
            .containsEntry(PROP_WORKFLOW_INSTANCE_KEY, resp.key())
            .containsEntry(PROP_WORKFLOW_ACTIVITY_ID, "bar")
            .containsEntry(PROP_WORKFLOW_VERSION, 2);
    }

    @Test
    public void shouldCreatePreviousVersionOfWorkflowInstance()
    {
        // given
        testClient.deploy(Bpmn.createExecutableProcess("process")
                .startEvent("foo")
                .endEvent()
                .done());

        testClient.deploy(Bpmn.createExecutableProcess("process")
                .startEvent("bar")
                .endEvent()
                .done());

        // when
        final ExecuteCommandResponse resp = testClient.sendCreateWorkflowInstanceRequest("process", 1);

        // then
        final SubscribedEvent event = testClient.receiveSingleEvent(workflowInstanceEvents(START_EVENT_OCCURRED.name()));

        assertThat(event.event())
            .containsEntry(PROP_WORKFLOW_BPMN_PROCESS_ID, "process")
            .containsEntry(PROP_WORKFLOW_INSTANCE_KEY, resp.key())
            .containsEntry(PROP_WORKFLOW_ACTIVITY_ID, "foo")
            .containsEntry(PROP_WORKFLOW_VERSION, 1);
    }

    @Test
    public void shouldCreateWorkflowInstanceWithPayload()
    {
        // given
        testClient.deploy(Bpmn.createExecutableProcess("process")
            .startEvent()
            .endEvent()
            .done());

        // when
        final ExecuteCommandResponse resp = testClient
            .sendCreateWorkflowInstanceRequest("process", PAYLOAD);

        // then
        assertThat(resp.key()).isGreaterThanOrEqualTo(0L);
        assertThat(resp.topicId()).isEqualTo(0L);
        assertThat(resp.getEvent())
            .containsEntry(PROP_EVENT_TYPE, WORKFLOW_INSTANCE_CREATED.name())
            .containsEntry(PROP_WORKFLOW_BPMN_PROCESS_ID, "process")
            .containsEntry(PROP_WORKFLOW_INSTANCE_KEY, resp.key())
            .containsEntry(PROP_WORKFLOW_VERSION, 1)
            .containsEntry(PROP_WORKFLOW_PAYLOAD, PAYLOAD);
    }


}

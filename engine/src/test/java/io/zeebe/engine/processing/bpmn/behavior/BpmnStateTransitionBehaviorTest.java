package io.zeebe.engine.processing.bpmn.behavior;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.zeebe.engine.metrics.WorkflowEngineMetrics;
import io.zeebe.engine.processing.bpmn.BpmnElementContainerProcessor;
import io.zeebe.engine.processing.bpmn.BpmnElementContext;
import io.zeebe.engine.processing.bpmn.WorkflowInstanceStateTransitionGuard;
import io.zeebe.engine.processing.deployment.model.element.ExecutableFlowElement;
import io.zeebe.engine.processing.streamprocessor.StreamAppender;
import io.zeebe.engine.state.KeyGenerator;
import io.zeebe.protocol.impl.record.value.workflowinstance.WorkflowInstanceRecord;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import io.zeebe.protocol.record.value.BpmnElementType;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BpmnStateTransitionBehaviorTest {

  private BpmnStateTransitionBehavior sut;

  private BpmnStateTransitionBehavior spySut;

  @Mock StreamAppender mockStreamAppender;

  @BeforeEach
  public void setUp() {
    final KeyGenerator keyGenerator = null;
    final BpmnStateBehavior stateBehavior = null;
    final WorkflowEngineMetrics metrics = null;
    final WorkflowInstanceStateTransitionGuard stateTransitionGuard = null;
    final Function<BpmnElementType, BpmnElementContainerProcessor<ExecutableFlowElement>>
        processorLookUp = null;

    sut =
        new BpmnStateTransitionBehavior(
            mockStreamAppender,
            keyGenerator,
            stateBehavior,
            metrics,
            stateTransitionGuard,
            processorLookUp);

    spySut = spy(sut);
  }

  @Test
  public void testTransitionToActivatingShouldAppendFollowUpEventWithIntentElementActivating() {
    // given
    final var elementInstanceKey = 123l;
    final var context = mock(BpmnElementContext.class);
    final var record = mock(WorkflowInstanceRecord.class);
    when(context.getElementInstanceKey()).thenReturn(elementInstanceKey);
    when(context.getIntent()).thenReturn(WorkflowInstanceIntent.ACTIVATE_ELEMENT);
    when(context.getRecordValue()).thenReturn(record);

    // when
    spySut.transitionToActivating(context);

    // then
    verify(mockStreamAppender)
        .appendFollowUpEvent(elementInstanceKey, WorkflowInstanceIntent.ELEMENT_ACTIVATING, record);
  }
}

package io.zeebe.engine.processing.bpmn.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.zeebe.engine.processing.bpmn.BpmnElementContextImpl;
import io.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.zeebe.engine.processing.bpmn.behavior.BpmnEventSubscriptionBehavior;
import io.zeebe.engine.processing.bpmn.behavior.BpmnStateBehavior;
import io.zeebe.engine.processing.bpmn.behavior.BpmnStateTransitionBehavior;
import io.zeebe.engine.processing.bpmn.behavior.BpmnVariableMappingBehavior;
import io.zeebe.engine.processing.common.ExpressionProcessor;
import io.zeebe.engine.processing.deployment.model.element.ExecutableServiceTask;
import io.zeebe.engine.processing.streamprocessor.writers.TypedCommandWriter;
import io.zeebe.engine.state.KeyGenerator;
import io.zeebe.protocol.impl.record.value.workflowinstance.WorkflowInstanceRecord;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import io.zeebe.util.Either;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServiceTaskProcessorTest {

  @Mock KeyGenerator mockKeyGenerator;
  @Mock BpmnBehaviors mockBehaviors;
  @Mock BpmnStateTransitionBehavior mockStateTransitionBehavior;
  @Mock BpmnVariableMappingBehavior mockVariableMappingBehavior;
  @Mock BpmnEventSubscriptionBehavior mockEventSubscriptionBehavior;
  @Mock ExpressionProcessor mockExpressionProcessor;
  @Mock TypedCommandWriter mockCommandWriter;
  @Mock BpmnStateBehavior mockStateBehavior;

  private ServiceTaskProcessor sut;

  @BeforeEach
  public void setUp() {
    when(mockBehaviors.keyGenerator()).thenReturn(mockKeyGenerator);
    when(mockBehaviors.stateTransitionBehavior()).thenReturn(mockStateTransitionBehavior);
    when(mockBehaviors.variableMappingBehavior()).thenReturn(mockVariableMappingBehavior);
    when(mockBehaviors.eventSubscriptionBehavior()).thenReturn(mockEventSubscriptionBehavior);
    when(mockBehaviors.expressionBehavior()).thenReturn(mockExpressionProcessor);
    when(mockBehaviors.commandWriter()).thenReturn(mockCommandWriter);
    when(mockBehaviors.stateBehavior()).thenReturn(mockStateBehavior);

    sut = new ServiceTaskProcessor(mockBehaviors);
  }

  @Test
  public void testHappyPath() {
    // given
    final var elementId = "service-task";
    final var directBuffer = Mockito.mock(DirectBuffer.class);

    final var element = mock(ExecutableServiceTask.class);
    when(element.getEncodedHeaders()).thenReturn(directBuffer);
    when(element.getId()).thenReturn(wrapString(elementId));

    final var recordValue = Mockito.mock(WorkflowInstanceRecord.class);
    when(recordValue.getBpmnProcessIdBuffer()).thenReturn(wrapString("process"));
    //    when(recordValue.getElementIdBuffer()).thenReturn(wrapString(elementId));

    final var context = new BpmnElementContextImpl();
    context.init(0l, recordValue, WorkflowInstanceIntent.ACTIVATE_ELEMENT);

    final var context2 = context.copy(0l, recordValue, WorkflowInstanceIntent.ELEMENT_ACTIVATING);

    when(mockVariableMappingBehavior.applyInputMappings(Mockito.any(), Mockito.any()))
        .thenReturn(Either.right(null));
    when(mockEventSubscriptionBehavior.subscribeToEvents(Mockito.any(), Mockito.any()))
        .thenReturn(Either.right(null));

    when(mockExpressionProcessor.evaluateStringExpression(Mockito.any(), Mockito.anyLong()))
        .thenReturn(Either.right("job"));

    when(mockExpressionProcessor.evaluateLongExpression(Mockito.any(), Mockito.anyLong()))
        .thenReturn(Either.right(3l));

    // when
    sut.onActivate(element, context);

    // then
    verify(mockStateTransitionBehavior).transitionToActivating(context);
    verify(mockStateTransitionBehavior).transitionToActivated(context2);

    verifyNoInteractions(mockStateBehavior);
  }

  private DirectBuffer wrapString(final String input) {
    final var resultView = new UnsafeBuffer();
    resultView.wrap(input.getBytes());
    return resultView;
  }
}

/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.app.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Used to annotate a method that should be regular executed after the testbed has been started.
 * <p>
 * The scheduled method becomes enabled first after the given initial delay, and subsequently with the given period;
 * that is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
 * <tt>initialDelay + 2 * period</tt>, and so on. If any execution of the task encounters an exception, subsequent
 * executions are suppressed. Otherwise, the task will only terminate when the container is shutdown. If any execution
 * of this task takes longer than its period, then subsequent executions may start late, but will not concurrently
 * execute.
 * <p>
 * 
 * <pre>
 * public class PrintDate {
 * 
 *     &#064;ScheduleWithFixedDelay(1000)
 *     public void printDate() throws InterruptedException {
 *         Thread.sleep(1000);
 *         System.out.println(new Date());
 *     }
 * 
 * </pre>
 * <p>
 * The scheduling semantics of this method are identical to that of
 * {@link ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}.
 * 
 * @author Kasper Nielsen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScheduleWithFixedDelay {

    long initialDelay() default 0;

    /**
     * Returns the name of the task.
     * 
     * @return the name of the task
     */
    String name() default "";

    /**
     * Returns the time unit of the value parameter.
     * 
     * @return the time unit of the value parameter
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * Returns the delay between the termination of one execution and the commencement of the next.
     * 
     * @return the delay between the termination of one execution and the commencement of the next
     */
    long value();
}

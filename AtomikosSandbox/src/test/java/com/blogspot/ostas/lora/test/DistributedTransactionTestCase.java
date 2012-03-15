package com.blogspot.ostas.lora.test;

import com.blogspot.ostas.lora.test.cases.DistributedTransactionDatabaseFailure;
import com.blogspot.ostas.lora.test.cases.DistributedTransactionJmsFailure;
import com.blogspot.ostas.lora.test.cases.DistributedTransactionWithoutFailure;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DistributedTransactionWithoutFailure.class,
        DistributedTransactionDatabaseFailure.class,
        DistributedTransactionJmsFailure.class
})
public class DistributedTransactionTestCase{
}

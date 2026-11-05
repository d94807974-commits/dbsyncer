/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle.logminer.parser;

import java.util.List;

public interface Parser {

    List<Object> parseColumns();
}

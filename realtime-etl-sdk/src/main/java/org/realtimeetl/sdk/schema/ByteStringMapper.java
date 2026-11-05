/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.schema;

import java.nio.ByteBuffer;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 00:12
 */
public interface ByteStringMapper {

    void apply(ByteBuffer buffer);

}
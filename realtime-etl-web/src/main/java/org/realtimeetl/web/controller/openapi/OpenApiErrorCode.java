/**
 * Copyright 2026 Realtime ETL
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
package org.realtimeetl.web.controller.openapi;

/**
 * OpenAPI HarmonizationErrorCode Constant
 *
 * @author Realtime ETL.
 * @version 2.0.0
 */
public final class OpenApiErrorCode {

    private OpenApiErrorCode() {
    }

    /** Request parametersError（LikeParameterMissing, FormattingError、TimeStab/Nonce Verify INTRODUCTIONFailedWait.*/
    public static final int BAD_REQUEST = 400;

    /** NotAuthorization（LikeToken Is empty,NoneExpiry, expiry, orsecret Error） */
    public static final int UNAUTHORIZED = 401;

    /** Access is prohibited (e.g.IP Not White List)*/
    public static final int FORBIDDEN = 403;

    /** Resourceor capacity not open (e.g.API） */
    public static final int NOT_FOUND = 404;

    /** Method of request not permitted (if used)POST Request only supportedGET Yes.Interface） */
    public static final int METHOD_NOT_ALLOWED = 405;

    /** In Truly ServiceError（LikeLogin/Refresh/ProcessingError） */
    public static final int INTERNAL_ERROR = 500;

    /** Service not available for the time being（If preloadedFinish） */
    public static final int SERVICE_UNAVAILABLE = 503;
}

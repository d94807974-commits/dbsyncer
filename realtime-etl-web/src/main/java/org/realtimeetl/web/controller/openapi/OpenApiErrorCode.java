/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.controller.openapi;

/**
 * OpenAPI HarmonizationErrorCode Constant
 *
 * @author The clouds.
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

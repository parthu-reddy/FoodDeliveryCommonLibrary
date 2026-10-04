package com.fooddelivery.common.enums;

import com.fooddelivery.common.exception.IllegalStateTransitionException;

/** Provider checks and an administrator decision are distinct steps for every partner. */
public enum ApplicationStatus {
    DRAFT, SUBMITTED, IN_REVIEW, APPROVED, REJECTED, SUSPENDED;

    public static void requireTransition(ApplicationStatus from, ApplicationStatus to) {
        boolean allowed = from != null && to != null && switch (from) {
            case DRAFT, REJECTED -> to == SUBMITTED;
            case SUBMITTED -> to == IN_REVIEW || to == REJECTED;
            case IN_REVIEW -> to == APPROVED || to == REJECTED;
            case APPROVED -> to == SUSPENDED;
            case SUSPENDED -> to == APPROVED;
        };
        if (!allowed) {
            throw new IllegalStateTransitionException("Application cannot move from " + from + " to " + to);
        }
    }
}

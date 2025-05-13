package eu.vendeli.rethis.utils

enum class ClusterEventNames(val literal: String) {
    PLUS_RESET_MASTER("+reset-master"),
    PLUS_SLAVE("+slave"),
    PLUS_FAILOVER_STATE_RECONF_SLAVES("+failover-state-reconf-slaves"),
    PLUS_FAILOVER_DETECTED("+failover-detected"),
    PLUS_SLAVE_RECONF_SENT("+slave-reconf-sent"),
    PLUS_SLAVE_RECONF_INPROG("+slave-reconf-inprog"),
    PLUS_SLAVE_RECONF_DONE("+slave-reconf-done"),
    MINUS_DUP_SENTINEL("-dup-sentinel"),
    PLUS_SENTINEL("+sentinel"),
    PLUS_SDOWN("+sdown"),
    MINUS_SDOWN("-sdown"),
    PLUS_ODOWN("+odown"),
    MINUS_ODOWN("-odown"),
    PLUS_NEW_EPOCH("+new-epoch"),
    PLUS_TRY_FAILOVER("+try-failover"),
    PLUS_ELECTED_LEADER("+elected-leader"),
    PLUS_FAILOVER_STATE_SELECT_SLAVE("+failover-state-select-slave"),
    NO_GOOD_SLAVE("no-good-slave"),
    SELECTED_SLAVE("selected-slave"),
    FAILOVER_STATE_SEND_SLAVEOF_NOONE("failover-state-send-slaveof-noone"),
    FAILOVER_END_FOR_TIMEOUT("failover-end-for-timeout"),
    FAILOVER_END("failover-end"),
    SWITCH_MASTER("switch-master"),
    PLUS_TILT("+tilt"),
    MINUS_TILT("-tilt")
}

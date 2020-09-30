 
class Status:
    # DEFAULT
    STATUS_IDLE = '{"status":"idle"}'.encode()

    # MODE
    STATUS_EXPLORING = '{"status":"exploring"}'.encode()
    STATUS_FASTEST_PATH = '{"status":"fastest path"}'.encode()

    # MOVEMENTS
    STATUS_MOVING_FORWARD = '{"status":"moving forward"}'.encode()
    STATUS_TURNING_LEFT = '{"status":"turning left"}'.encode()
    STATUS_TURNING_RIGHT = '{"status":"turning right"}'.encode()
    STATUS_REVERSING = '{"status":"reversing"}'.encode()

    # ROBOT NECESSITIES 
    STATUS_TAKING_PICTURE = '{"status":"taking picture"}'.encode()
    STATUS_CALIBRATING_CORNER = '{"status":"calibrating corner"}'.encode()
    STATUS_SENSE_ALL = '{"status":"sense all"}'.encode()
    STATUS_ALIGN_RIGHT = '{"status":"align right"}'.encode()
    STATUS_ALIGN_FRONT = '{"status":"align front"}'.encode()
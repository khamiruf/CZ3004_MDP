import json

from src.Logger import Logger
from src.protocols import *

log = Logger()

arduino_commands = ['@S', 'FP'] #sensor only for now
arduino_out = ['SD', 'MC', 'CC', 'EC'] #TODO -- what kind of commands will Arduino be sending?

def format_for(target, payload):
    return json.dumps({
        'target': target,
        'payload': payload
    })

'''
Parse messages received from Arduino, essentially filters out
unnecessary messages
'''
def ardMsgParser(msg):
    #@S|888888, FP|command!
    data = msg.split('|', 1)
    
    if data[0] == 'FP':
        target = 'android'
        payload = data[1]

        return {
            'target': target,
            'payload': payload
        }
    elif data[0] == '@S':
        send_data = data[1]
        return send_data
    else:
        # for checklist
        # log.info("checklist: reading from Arduino")
        # return msg
        log.info("ardMsgParser never receive in proper format '@'")
        return None

'''
Parse messages received from PC, packages payload into
a JSON holding a target and payload
'''
def pcMsgParser(msg):
    data = msg.split('|', 1)
    command = data[0]
    
    target = None
    payload = msg

    if command == 'EC':
        target = 'both'
        payload = {
            'android': payload,
            'arduino': payload,
        }

    elif command == 'MDF':
        target = 'android'
        payload = payload[:-1] #just forward the string w/o '!' to android 

    # FP|ROFO1LO!
    elif command == 'FP':
        target = 'both'
        payload = {
            'android': data[1],
            'arduino': data[1][:-1],
        }
    
    # EX|R0|(x,y)!
    elif command == 'EX':
        cmd = data[1].split('|')
        target = 'all'
        payload = {
            'android': cmd[0], # only sending R0
            'arduino': cmd[0], # only sending R0
            'rpi': {
                'command': 'TP',
                'coord': cmd[1][:-1],
                }, # to take picture every step of exploration IMAGE REC
        }

    # OB|(x,y)|(x,y)|!
    elif command == 'OB':
        if data[1] is None:
            print("no obstacles")
            return "default"
        else:
            print("entered OB else branch")
            target = 'android'
            payload = payload + '!'

    # complete exploration
    elif command == 'N':
        target = 'both'
        payload = {
            'android': Status.STATUS_IDLE,
            'arduino': payload,
        }

    # image detection
    elif command == 'IM':
        target = 'android'
        payload = payload

    else:
        log.error('pcMsgParser unknown command: ' + str(command))
        print("payload from pcMsgParser else: ", payload)
        return payload
    
    return {
        'target': target,
        'payload': payload
    }

'''
Parse MDP message from PC to a format consumable by
Android or Arduino
'''
def mdfParser(system, mdf_string):
    mdf_data = mdf_string.split('|')
    if system == 'android':
        # include the ! end of string
        return '|'.join(mdf_data[0 : len(mdf_data)])

'''
Parse FP message from PC to a format consumable by
Android or Arduino
'''
def fpParser(system, path_data):
    #eg. command FP|ROF1LO!
    step_seq = []

    dir_move_map = {'F': 'F', 'R': 'R', 'L': 'L', 'B': 'B'} #TODO -- to be confirmed

    if payload[-1] == "!":
        payload = payload[:-2] #remove |!
        

    path_seq = path_data.split('|') #split by ;, can just split by | if pcMsgParser splits by (|,1)

    for step in path_seq:
        # handle 2 movement commands
        if (step == "L1"):
            step_seq = step_seq + list(dir_move_map['L'] + dir_move_map['F'])
        elif (step == "R1"):
            step_seq = step_seq + list(dir_move_map['R'] + dir_move_map['F'])
        elif (step == "B1"):
            step_seq = step_seq + list(dir_move_map['R'] + dir_move_map['R'] + dir_move_map['F'])
        elif (step == "L0"):
            step_seq = step_seq + list(dir_move_map['L'])
        elif (step == "R0"):
            step_seq = step_seq + list(dir_move_map['R'])
        else:
            next_step = list(str(step))
            dir = next_step[0]
            num_steps = int(next_step[1])
            
            step_seq = step_seq + [dir_move_map[next_step[0]] for x in range(next_step[1])]

    # can implement 2 diff parsing if arduino and android receives differently
    if system == 'android':
        return 'FP|{steps}'.format(steps=','.join(step_seq))
    if system == 'arduino': #TODO what is the format to send to arduino for Fastest Path
        step_seq = compressStepSeq(step_seq)
        return '{steps}'.format(steps=','.join(step_seq))

'''
Parse EX message from PC to a format consumable by
Android or Arduino
'''
# maybe not needed cause just passing the command from 
def exParser(system, path_data):
    #eg. command F6|!, EX|R1|!, EX|R0|!, EX|L1|! return F6|, R1|, R0|
    path_step = path_data.split("|")

    pass


'''
Compresses a list of step_seq in a format which
Arduino can consume
'''
def compressStepSeq(step_seq):
    forward_count = 0
    seq = []
    for step in step_seq:
        if step == 'F':
            forward_count = forward_count + 1
            if forward_count == 9:
                seq.append('F{x}'.format(x=forward_count))
                forward_count = 0
        else:
            seq.append('F{x}'.format(x=forward_count))
            seq.append(step)
            forward_count = 0
    if forward_count > 0:
        seq.append('F{x}'.format(x=forward_count))
    return seq
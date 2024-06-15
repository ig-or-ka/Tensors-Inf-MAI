from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.request import Request
from django.contrib.auth.models import User
from django.contrib.auth import authenticate
from .models import UpscaleTask, UpscaleTaskStatus
import jwt, time, datetime, queue
from .config import *
import asyncio, json, base64, requests
from websockets.client import connect
from threading import Thread


class Login(APIView):
    def post(self, request: Request):
        params = ('username','password')
        no_params = []

        for param in params:
            if param not in request.data:
                no_params.append(param)

        if len(no_params) > 0:                
            return Response({'msg':'Missing parameters: ' + ', '.join(no_params)}, 400)

        user = authenticate(username=request.data['username'], password=request.data['password'])

        if user is None:
            return Response({'msg':'Invalid credentials'}, 401)

        json_data = {
            'username': request.data['username'],
            'time': int(time.time())
        }

        token = jwt.encode(payload=json_data, key=JWT_SECRET, algorithm="HS256")

        return Response({'access_token':token})
    

class Signup(APIView):
    def post(self, request: Request):
        params = ('username','password')
        no_params = []

        for param in params:
            if param not in request.data:
                no_params.append(param)

        if len(no_params) > 0:                
            return Response({'msg':'Missing parameters: ' + ', '.join(no_params)}, 400)
        
        usrs = User.objects.filter(username=request.data['username'])

        if len(usrs) > 0:
            return Response({'msg':'User already exists'}, 400)
        
        User.objects.create_user(
            request.data['username'],
            password=request.data['password'],
        )
        
        json_data = {
            'username': request.data['username'],
            'time': int(time.time())
        }

        token = jwt.encode(payload=json_data, key=JWT_SECRET, algorithm="HS256")
        return Response({'access_token':token})
    

def check_token(params):
    if 'token' in params:
        try:
            data = jwt.decode(params['token'], JWT_SECRET, algorithms=["HS256"])

            if time.time() - data['time'] > TIME_EXPIRE:
                raise Exception("The token has expired")
            
            return data['username']
            
        except:
            raise Exception("Invalid credentials")

    else:
        raise Exception("Token required")
    

class VARS:
    tasks_queue = asyncio.Queue()
    update_task_queue = queue.Queue()
    loop: asyncio.AbstractEventLoop = None
 

async def upscalePhotoReq(task_id, base64_photo):
    print("StartTask:", task_id)
    dat = {"data":["task(ltmpcya215dw2by)",0,base64_photo,None,"","",True,True,0,4,0,512,512,True,"ESRGAN_4x","None",0,False,1,False,1,0,False,0.5,0.2,False,0.9,0.15,0.5,False,False,384,768,4096,409600,"Maximize area",0.1,False,["Horizontal"],False,["Deepbooru"],None,"<p>Postprocess upscale by: 4, Postprocess upscaler: ESRGAN_4x</p>","<div class='performance'><p class='time'>Time taken: <wbr><span class='measurement'>17.4 sec.</span></p><p class='vram'><abbr title='Active: peak amount of video memory used during generation (excluding cached data)'>A</abbr>: <span class='measurement'>2.02 GB</span>, <wbr><abbr title='Reserved: total amount of video memory allocated by the Torch library '>R</abbr>: <span class='measurement'>2.43 GB</span>, <wbr><abbr title='System: peak amount of video memory allocated by all running programs, out of total capacity'>Sys</abbr>: <span class='measurement'>4.0/4 GB</span> (100.0%)</p></div>"],"event_data":True,"fn_index":331,"session_hash":"gw72kl2fhf"}

    try:
        async with connect("ws://127.0.0.1:7860/queue/join") as websocket:
            while True:
                message = await websocket.recv()
                js_msg = json.loads(message)

                if js_msg["msg"] == "send_hash":
                    dt = {"fn_index":0,"session_hash":"gw72kl2fhf"}
                    await websocket.send(json.dumps(dt))

                elif js_msg["msg"] == "send_data":
                    await websocket.send(json.dumps(dat))

                elif js_msg["msg"] == "process_completed":
                    filename = js_msg["output"]["data"][0][0]["name"]
                    url_get_res = f"http://127.0.0.1:7860/file={filename}"

                    VARS.update_task_queue.put_nowait((task_id, UpscaleTaskStatus.done, url_get_res))
                    break

                else:
                    print(f"Received: {message}")

    except:
        VARS.update_task_queue.put_nowait((task_id, UpscaleTaskStatus.error, ""))


async def tasks_worker():
    while True:
        task_id, photo = await VARS.tasks_queue.get()
        asyncio.get_running_loop().create_task(upscalePhotoReq(task_id, photo))


def update_tasks_thread():
    while True:
        task_id, status, image_url = VARS.update_task_queue.get()
        print(task_id, status, image_url)

        task = UpscaleTask.objects.filter(id=task_id)[0]
        task.status = status.value

        if status == UpscaleTaskStatus.done:
            file_data = requests.get(image_url).content
            task.output_photo = base64.encodebytes(file_data).decode()

        task.save()


def tasks_thread():
    VARS.loop = asyncio.new_event_loop()
    VARS.loop.run_until_complete(tasks_worker())


Thread(target=tasks_thread, daemon=True).start()
Thread(target=update_tasks_thread, daemon=True).start()


class UpscalePhoto(APIView):
    def post(self, request: Request):
        try:
            username = check_token(request.data)

        except Exception as ex:
            return Response({'error': str(ex)}, 401)
        
        user = User.objects.filter(username=username)[0]

        task = UpscaleTask(
            user=user,
            input_photo=request.data['photo'],
            time=datetime.datetime.now(),
            day=datetime.date.today()
        )
        task.save()
        
        VARS.loop.call_soon_threadsafe(VARS.tasks_queue.put_nowait, (task.id, request.data['photo']))

        return Response({'task_id':str(task.id)})
    

class UpscaleTaskInfo(APIView):
    def post(self, request: Request):
        filter_tasks = UpscaleTask.objects.filter(id=request.data["task_id"])

        if filter_tasks.count() == 0:
            return Response({'msg':'task not found'}, 400)
        
        task = filter_tasks[0]

        return Response({'status':str(task.status), 'output_photo':task.output_photo})
    

class UpscaleTasksList(APIView):
    def post(self, request: Request):
        try:
            username = check_token(request.data)

        except Exception as ex:
            return Response({'error': str(ex)}, 401)
        
        user = User.objects.filter(username=username)[0]

        tasks = UpscaleTask.objects.filter(user=user)
        tasks_list = []

        for task in tasks:
            tasks_list.append({
                'task_id':task.id,
                'time':f'{task.day.isoformat()} {task.time.isoformat()}',
                'status':task.status
            })

        return Response(tasks_list)
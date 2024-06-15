from django.urls import path
from .views import *


urlpatterns = [
    path('signup', Signup.as_view()),
    path('login', Login.as_view()),
    path('upscale_photo', UpscalePhoto.as_view()),
    path('upscale_task_info', UpscaleTaskInfo.as_view()),
    path('user_tasks_list', UpscaleTasksList.as_view()),
]
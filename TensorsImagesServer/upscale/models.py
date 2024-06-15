from django.db import models
from django.contrib.auth.models import User
import enum


class UserRole(enum.Enum):
    user = 0
    maker = 1


class UpscaleTaskStatus(enum.Enum):
    new = 0
    done = 1
    error = 2


class UpscaleTask(models.Model):
    user = models.ForeignKey(User, models.PROTECT)
    input_photo = models.TextField()
    output_photo = models.TextField(default="")
    status = models.IntegerField(default=0)
    day = models.DateField()
    time = models.TimeField()
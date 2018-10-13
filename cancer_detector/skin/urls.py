from django.urls import path, include

from . import views

app_name = 'skin'
urlpatterns = [
    path('', views.index, name='index'),
    path('skin_cancer/detect', views.analyze_image, name='analyze_restful')
]
